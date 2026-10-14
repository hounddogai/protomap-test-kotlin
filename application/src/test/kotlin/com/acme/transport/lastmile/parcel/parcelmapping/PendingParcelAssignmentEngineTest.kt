package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingParcelAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingParcelAssignmentEngine(newRegistry())
        val item = newPendingParcelAssignment("code-152", "title-526")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
