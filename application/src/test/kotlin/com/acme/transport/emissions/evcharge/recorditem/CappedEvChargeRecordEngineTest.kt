package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedEvChargeRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedEvChargeRecordEngine(newRegistry())
        val item = newCappedEvChargeRecord("code-573", "label-633")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
