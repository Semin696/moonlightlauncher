package moonlight.launch.startup.session;

import org.jetbrains.annotations.NotNull;

public record Session(String year, String build) {

    @Override
    public @NotNull String toString() {
        return year.concat(".").concat(build);
    }

}
