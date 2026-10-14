package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingAccessorialGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingAccessorialGroupEngine(newRegistry())
        val item = newPendingAccessorialGroup("code-752", "name-333")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
