package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorManifestEngine(newRegistry())
        val item = newDockDoorManifest("code-258", "label-831")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
