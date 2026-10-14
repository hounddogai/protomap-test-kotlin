package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationTargetEngine(newRegistry())
        val item = newQualificationTarget("code-798", "title-724")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
