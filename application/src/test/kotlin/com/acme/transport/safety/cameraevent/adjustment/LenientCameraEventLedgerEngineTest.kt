package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCameraEventLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCameraEventLedgerEngine(newRegistry())
        val item = newLenientCameraEventLedger("code-758", "label-623")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
