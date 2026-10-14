package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTenderRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTenderRollupEngine(newRegistry())
        val item = newManualTenderRollup("code-688", "title-515")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
