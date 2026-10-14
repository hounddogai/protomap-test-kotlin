package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedIdlingVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedIdlingVarianceEngine(newRegistry())
        val item = newBatchedIdlingVariance("code-319", "name-21")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
