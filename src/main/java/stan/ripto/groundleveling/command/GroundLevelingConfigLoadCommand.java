package stan.ripto.groundleveling.command;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;
import stan.ripto.groundleveling.event.GroundLevelingConfigLoadHelper;

import java.nio.file.Path;

public class GroundLevelingConfigLoadCommand {
    private static final String COMMAND_MESSAGE_SUCCESS_KEY = "command.result.groundleveling.success";
    private static final String COMMAND_MESSAGE_FAIL_KEY = "command.result.groundleveling.fail";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("groundleveling")
                .executes(context -> {
                    if (reloadCommonConfig()) {
                        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_MESSAGE_SUCCESS_KEY), true);
                    } else {
                        context.getSource().sendFailure(Component.translatable(COMMAND_MESSAGE_FAIL_KEY));
                    }
                    return 1;
                })
        );
    }

    private static boolean reloadCommonConfig() {
        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve("groundleveling-common.toml");
            CommentedFileConfig data = CommentedFileConfig.builder(path).autosave().sync().writingMode(WritingMode.REPLACE).build();
            data.load();
            GroundLevelingConfigs.COMMON_CONFIG.setConfig(data);
            GroundLevelingConfigLoadHelper.loadConfig();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getCommandMessageSuccessKey() {
        return COMMAND_MESSAGE_SUCCESS_KEY;
    }

    public static String getCommandMessageFailKey() {
        return COMMAND_MESSAGE_FAIL_KEY;
    }
}
