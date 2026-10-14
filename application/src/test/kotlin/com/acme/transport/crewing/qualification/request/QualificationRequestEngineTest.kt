package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationRequestEngine(newRegistry())
        val item = newQualificationRequest("code-635", "label-278")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
