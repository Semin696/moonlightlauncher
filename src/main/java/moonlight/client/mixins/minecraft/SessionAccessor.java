package moonlight.client.mixins.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.session.Session;

import java.util.UUID;

@Mixin(Session.class)
public interface SessionAccessor {

    @Mutable
    @Accessor("username")
    void setUsername(String username);

    @Mutable
    @Accessor("uuid")
    void setUuid(UUID uuid);

}
