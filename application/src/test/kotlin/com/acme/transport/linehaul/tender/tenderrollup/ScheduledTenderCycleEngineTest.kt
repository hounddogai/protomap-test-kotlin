package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTenderCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledTenderCycleEngine(newRegistry())
        val item = newScheduledTenderCycle("code-409", "title-439")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
