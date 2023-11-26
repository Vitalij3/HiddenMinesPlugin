package salatosik.hiddenmines.listener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Listener;
import salatosik.hiddenmines.configuration.PluginConfiguration;

@AllArgsConstructor
@Getter
public class BasePluginListener implements Listener {
    private final PluginConfiguration configuration;
}
