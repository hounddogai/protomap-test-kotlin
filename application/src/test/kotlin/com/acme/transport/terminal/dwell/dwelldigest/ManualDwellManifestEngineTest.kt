package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDwellManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDwellManifestEngine(newRegistry())
        val item = newManualDwellManifest("code-758", "name-571")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
