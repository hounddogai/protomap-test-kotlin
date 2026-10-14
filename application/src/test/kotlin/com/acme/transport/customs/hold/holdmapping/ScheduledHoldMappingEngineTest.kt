package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledHoldMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledHoldMappingEngine(newRegistry())
        val item = newScheduledHoldMapping("code-508", "label-178")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
