package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTenderEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTenderEntryEngine(newRegistry())
        val item = newPrimaryTenderEntry("code-811", "title-818")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
