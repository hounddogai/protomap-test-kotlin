package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingQualificationNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingQualificationNoticeEngine()
        val item = newRollingQualificationNotice("code-919", "name-875")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
