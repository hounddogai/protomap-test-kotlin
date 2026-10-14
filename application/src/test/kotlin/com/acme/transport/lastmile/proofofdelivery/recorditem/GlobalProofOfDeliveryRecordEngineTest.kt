package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalProofOfDeliveryRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalProofOfDeliveryRecordEngine(newRegistry())
        val item = newGlobalProofOfDeliveryRecord("code-657", "title-20")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
