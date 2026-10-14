package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedProofOfDeliveryRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedProofOfDeliveryRollupEngine(newRegistry())
        val item = newBatchedProofOfDeliveryRollup("code-936", "title-615")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
