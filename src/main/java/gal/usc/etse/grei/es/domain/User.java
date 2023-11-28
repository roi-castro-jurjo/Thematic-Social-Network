package gal.usc.etse.grei.es.domain;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import gal.usc.etse.grei.es.domain.Date;

@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "User",
        description = "A complete user representation"
)
public class User {
    @Id
    @NotBlank(message = "The email field can not be empty")
    @Email
    @Schema(required = true, example = "test@test.com")
    private String email;
    @Schema(example = "Pepe Perez")
    private String name;
    @Schema(example = "Spain")
    private String country;
    @Schema(example = "https://placekitten.com/200/287")
    private String picture;
    private Date birthday;
    private String password;
    private List<String> roles;

    public User() {}

    public User(String email, String name, String country, String picture, Date birthday, String password, List<String> roles) {
        this.email = email;
        this.name = name;
        this.country = country;
        this.picture = picture;
        this.birthday = birthday;
        this.password = password;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getCountry() {
        return country;
    }
    public String getPicture() {
        return picture;
    }
    public Date getBirthday() {
        return birthday;
    }
    public String getPassword() {
        return password;
    }
    public List<String> getRoles() {
        return roles;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }
    public User setName(String name) {
        this.name = name;
        return this;
    }
    public User setCountry(String country) {
        this.country = country;
        return this;
    }
    public User setPicture(String picture) {
        this.picture = picture;
        return this;
    }
    public User setBirthday(Date birthday) {
        this.birthday = birthday;
        return this;
    }
    public User setPassword(String password) {
        this.password = password;
        return this;
    }
    public User setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email) && Objects.equals(name, user.name) && Objects.equals(country, user.country) && Objects.equals(picture, user.picture) && Objects.equals(birthday, user.birthday) && Objects.equals(password, user.password) && Objects.equals(roles, user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, name, country, picture, birthday, password, roles);
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", picture='" + picture + '\'' +
                ", birthday=" + birthday +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                '}';
    }
}
