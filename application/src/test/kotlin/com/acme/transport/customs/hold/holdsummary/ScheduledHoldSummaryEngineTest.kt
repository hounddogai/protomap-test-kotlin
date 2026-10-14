package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledHoldSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledHoldSummaryEngine(newRegistry())
        val item = newScheduledHoldSummary("code-93", "name-655")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
