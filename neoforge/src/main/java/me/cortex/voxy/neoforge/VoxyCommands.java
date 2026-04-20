package me.cortex.voxy.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class VoxyCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("voxy")
            .requires(source -> source.hasPermission(0))
            
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> 
                        Component.literal("Voxy配置已重新加载"), false);
                    return 1;
                }))
            
            .then(Commands.literal("distance")
                .then(Commands.argument("blocks", IntegerArgumentType.integer(8, 127))
                    .executes(ctx -> {
                        int distance = IntegerArgumentType.getInteger(ctx, "blocks");
                        ctx.getSource().sendSuccess(() -> 
                            Component.literal("区块请求距离设置为: " + distance), false);
                        return 1;
                    })))
            
            .then(Commands.literal("status")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> 
                        Component.literal("Voxy状态: 已加载"), false);
                    return 1;
                }))
        );
    }
}