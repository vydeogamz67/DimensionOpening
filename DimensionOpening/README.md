# DimensionOpening Plugin

A Minecraft Paper 1.21 plugin that allows server operators to control access to different dimensions (Overworld, Nether, End).

## Features

- **Dimension Control**: Open and close access to Overworld, Nether, and End dimensions
- **Operator Only**: Only server operators can control dimensions
- **Portal Blocking**: Prevents players from using portals to closed dimensions
- **Teleportation Prevention**: Blocks all teleportation attempts to closed dimensions
- **Persistent States**: Dimension states are saved and restored on server restart
- **Configurable Messages**: Customize all plugin messages
- **Broadcast Notifications**: Optional server-wide announcements when dimensions change

## Commands

- `/dimension open <world|nether|end>` - Opens the specified dimension
- `/dimension close <world|nether|end>` - Closes the specified dimension

### Examples
```
/dimension close nether    # Closes the Nether dimension
/dimension open end        # Opens the End dimension
/dimension close world     # Closes the Overworld dimension
```

## Permissions

- `dimensionopening.admin` - Allows access to dimension control commands (default: op)

## Installation

1. Download the latest release JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/DimensionOpening/config.yml` if needed

## Building from Source

### Requirements
- Java 17 or higher
- Maven 3.6 or higher

### Build Steps
```bash
git clone <repository-url>
cd DimensionOpening
mvn clean package
```

The compiled JAR will be in the `target` directory.

## Configuration

The plugin creates a `config.yml` file with the following options:

```yaml
dimensions:
  overworld:
    open: true
  nether:
    open: true
  end:
    open: true

settings:
  broadcast_changes: true
  ops_bypass_restrictions: true
  messages:
    dimension_closed: "&cThe %dimension% dimension is currently closed!"
    dimension_opened: "&aThe %dimension% dimension is now accessible!"
    # ... more message options
```

## How It Works

1. **Dimension States**: The plugin tracks whether each dimension is open or closed
2. **Event Listening**: Monitors player teleportation and portal usage events
3. **Access Control**: Blocks access to closed dimensions for non-operators
4. **Persistence**: Saves dimension states to config file for server restarts

## Compatibility

- **Minecraft Version**: 1.21
- **Server Software**: Paper (recommended), Spigot
- **Java Version**: 17+

## Support

If you encounter any issues or have feature requests, please create an issue on the project repository.

## License

This project is licensed under the MIT License.