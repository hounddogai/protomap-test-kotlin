package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalQualificationDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalQualificationDigestEngine()
        val item = newGlobalQualificationDigest("code-546", "name-363")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
