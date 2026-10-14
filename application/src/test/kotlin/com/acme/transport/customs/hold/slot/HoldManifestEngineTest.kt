package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldManifestEngine(newRegistry())
        val item = newHoldManifest("code-482", "name-868")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
