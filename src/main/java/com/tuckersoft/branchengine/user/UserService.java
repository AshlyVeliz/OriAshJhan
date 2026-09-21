package com.tuckersoft.branchengine.user;

import com.tuckersoft.branchengine.error.ApiException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    /** Usuario dueño del token de la peticion actual. */
    public User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw ApiException.unauthorized("Autenticacion requerida");
        }
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Autenticacion requerida"));
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return UserResponse.from(currentUser());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse changeRole(Long id, String role) {
        User target = users.findById(id)
                .orElseThrow(() -> ApiException.notFound("Usuario " + id + " no existe"));
        if (!User.ROLE_USER.equals(role) && !User.ROLE_ADMIN.equals(role)) {
            throw ApiException.badRequest("El rol debe ser ROLE_USER o ROLE_ADMIN");
        }
        if (target.getId().equals(currentUser().getId())) {
            throw ApiException.badRequest("Un administrador no puede cambiar su propio rol");
        }
        target.setRole(role);
        return UserResponse.from(users.save(target));
    }
}
