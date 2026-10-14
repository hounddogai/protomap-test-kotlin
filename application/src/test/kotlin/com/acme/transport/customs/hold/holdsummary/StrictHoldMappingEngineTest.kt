package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictHoldMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictHoldMappingEngine(newRegistry())
        val item = newStrictHoldMapping("code-219", "title-894")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
