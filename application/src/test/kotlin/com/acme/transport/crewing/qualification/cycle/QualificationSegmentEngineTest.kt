package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationSegmentEngine(newRegistry())
        val item = newQualificationSegment("code-698", "name-847")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
