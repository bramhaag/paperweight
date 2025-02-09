/*
 * paperweight is a Gradle plugin for the PaperMC project.
 *
 * Copyright (c) 2021 Kyle Wood (DenWav)
 *                    Contributors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 only, no later versions.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package io.papermc.paperweight.userdev.internal.setup

import io.papermc.paperweight.util.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

fun applyDevBundlePatches(
    decompiledJar: Path,
    devBundlePatches: Path,
    outputJar: Path
) {
    Git.checkForGit()

    val workDir = findOutputDir(outputJar)

    try {
        unzip(decompiledJar, workDir)
        val git = Git(workDir)

        Files.walk(devBundlePatches).use { stream ->
            stream.forEach {
                if (it.name.endsWith(".patch")) {
                    git("apply", "--ignore-whitespace", it.absolutePathString()).executeOut()
                } else if (it.isRegularFile()) {
                    val destination = workDir.resolve(it.relativeTo(devBundlePatches))
                    destination.parent.createDirectories()
                    it.copyTo(destination, overwrite = true)
                }
            }
        }

        ensureDeleted(outputJar)
        zip(workDir, outputJar)
    } finally {
        workDir.deleteRecursively()
    }
}
