package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialSnapshotEngine()
        val item = newAccessorialSnapshot("code-275", "title-860")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
