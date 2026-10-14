package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationThresholdEngine(newRegistry())
        val item = newQualificationThreshold("code-455", "name-256")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
