package com.pedrozc90;

import com.pedrozc90.core.authentication.AuthenticationContext;
import com.pedrozc90.core.models.Context;
import com.pedrozc90.core.models.Ping;
import com.pedrozc90.core.models.ResultContent;
import com.pedrozc90.tenants.models.Tenant;
import com.pedrozc90.tenants.repo.TenantRepository;
import com.pedrozc90.users.models.User;
import com.pedrozc90.users.repo.UserRepository;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

import java.security.Principal;
import java.util.Optional;

@Controller("/")
@Secured(SecurityRule.IS_ANONYMOUS)
public class IndexController {

    @Inject
    private UserRepository userRepository;

    @Inject
    private TenantRepository tenantRepository;

    @Get("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String index() {
        return "sanity check";
    }

    @Get("/ping")
    public Ping ping() {
        return new Ping();
    }

    @Get("/context")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public HttpResponse<ResultContent<Context>> context(final Authentication authentication) {
        final Long userId = AuthenticationContext.getUserId();      // AuthenticationUtils.getUserId(authentication);
        final Optional<User> userOpt = userRepository.findById(userId);

        final Long tenantId = AuthenticationContext.getTenantId();  // AuthenticationUtils.getTenantId(authentication);
        final Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);

        final Context context = new Context();
        userOpt.ifPresent(context::setUser);
        tenantOpt.ifPresent(context::setTenant);

        return HttpResponse.ok(ResultContent.of(context));
    }

    @Get("/secured")
    @Produces(MediaType.TEXT_PLAIN)
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public String secured(@Header(HttpHeaders.AUTHORIZATION) final String authorization, final Principal principal) {
        return principal.getName();
    }

}
