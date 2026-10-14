package com.acme.transport.crewing.qualification.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationOverrideEngine()
        val item = newQualificationOverride("code-76", "title-188")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
