package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedInspectionClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedInspectionClaimEngine(newRegistry())
        val item = newBatchedInspectionClaim("code-244", "name-284")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
