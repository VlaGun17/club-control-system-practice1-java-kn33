package validation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ValidationResult(Map<String, List<String>> errors) {

    public boolean isValid() {
        return errors.isEmpty();
    }

    @Override
    public Map<String, List<String>> errors() {
        return Collections.unmodifiableMap(errors);
    }

    public List<String> getFieldErrors(String field) {
        return errors.getOrDefault(field, Collections.emptyList());
    }

    public String getErrorMessage() {
        if (isValid()) {
            return "No errors";
        }

        StringBuilder sb = new StringBuilder();
        errors.forEach((field, messsages) -> {
            sb.append("  ").append(field).append(":\n");
            messsages.forEach(msg -> sb.append("   - ").append(msg).append("\n"));
        });
        return sb.toString();
    }

    @Override
    public String toString() {
        return getErrorMessage();
    }
}
