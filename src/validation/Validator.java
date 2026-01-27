package validation;

public interface Validator<T> {

    ValidationResult validate(T entity);
}
