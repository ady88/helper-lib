### TODO
 - should store command output and error to separate files - filenames should be group_commandName_std/err
     * needs update to write to proper std/err file
 - have Toggle terminal command

### DONE
 - have a cleaner separation for each command type (try to have the command interfaces in a separate package and each implementation in its own package) to see what are the classes that need to be implemented
 - run terminal command in a separate thread