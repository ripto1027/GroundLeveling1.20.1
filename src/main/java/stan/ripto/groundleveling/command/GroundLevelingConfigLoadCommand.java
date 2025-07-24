package stan.ripto.groundleveling.command;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;
import stan.ripto.groundleveling.datagen.lang.TranslateKeys;
import stan.ripto.groundleveling.util.GroundLevelingConfigLoadHandler;

public class GroundLevelingConfigLoadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("GroundLevelingConfigReload")
                .executes(GroundLevelingConfigLoadCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        ModConfig serverConfig = ConfigTracker.INSTANCE.fileMap().get("groundleveling-server.toml");
        if (serverConfig == null) {
            context.getSource().sendFailure(Component.translatable(TranslateKeys.COMMAND_RESULT_FAIL));
            return 0;
        }

        try {
            CommentedFileConfig configData =
                    CommentedFileConfig.builder(serverConfig.getFullPath())
                            .autosave().sync().writingMode(WritingMode.REPLACE).build();

            configData.load();
            GroundLevelingConfigs.SERVER_SPEC.setConfig(configData);
            GroundLevelingConfigLoadHandler.load();

            context.getSource().sendSuccess(() ->
                    Component.translatable(TranslateKeys.COMMAND_RESULT_SUCCESS), true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable(TranslateKeys.COMMAND_RESULT_FAIL));
            return 0;
        }
    }
}
