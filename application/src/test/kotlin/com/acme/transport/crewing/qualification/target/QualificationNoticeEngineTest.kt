package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationNoticeEngine(newRegistry())
        val item = newQualificationNotice("code-404", "label-15")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
