package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingQualificationAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingQualificationAllocationEngine(newRegistry())
        val item = newRollingQualificationAllocation("code-622", "title-345")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
