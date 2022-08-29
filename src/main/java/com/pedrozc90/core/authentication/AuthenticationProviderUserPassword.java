package com.pedrozc90.core.authentication;

import com.pedrozc90.core.utils.AuthenticationUtils;
import com.pedrozc90.users.models.User;
import com.pedrozc90.users.repo.UserRepository;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.codec.digest.DigestUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider {

    @Inject
    private UserRepository userRepo;

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable final HttpRequest<?> httpRequest,
                                                          final AuthenticationRequest<?, ?> authenticationRequest) {
        return Flux.create((emitter) -> {
            final String username = authenticationRequest.getIdentity().toString();
            final String password = authenticationRequest.getSecret().toString();

            final String passwordHashed = DigestUtils.md5Hex(password);

            final Optional<User> opt = userRepo.findByCredentials(username, passwordHashed);

            if (opt.isPresent()) {
                final User user = opt.get();
                if (user.isNotActive()) {
                    emitter.error(AuthenticationResponse.exception(AuthenticationFailureReason.ACCOUNT_LOCKED));
                } else {
                    final Map<String, Object> attributes = new HashMap<>();
                    AuthenticationUtils.setUserId(user, attributes);

                    emitter.next(AuthenticationResponse.success(username, attributes));
                }
                emitter.complete();
            } else {
                emitter.error(AuthenticationResponse.exception(AuthenticationFailureReason.USER_NOT_FOUND));
            }
        }, FluxSink.OverflowStrategy.ERROR);
    }

}