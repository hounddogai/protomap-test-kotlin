package com.acme.transport.freightpay.accessorial.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAccessorialBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAccessorialBatchEngine()
        val item = newTieredAccessorialBatch("code-208", "title-229")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
