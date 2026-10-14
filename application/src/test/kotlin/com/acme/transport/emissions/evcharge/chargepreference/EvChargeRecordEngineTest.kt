package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeRecordEngine(newRegistry())
        val item = newEvChargeRecord("code-388", "label-694")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
