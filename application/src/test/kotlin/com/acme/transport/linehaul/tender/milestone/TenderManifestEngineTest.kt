package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderManifestEngine(newRegistry())
        val item = newTenderManifest("code-579", "name-247")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
