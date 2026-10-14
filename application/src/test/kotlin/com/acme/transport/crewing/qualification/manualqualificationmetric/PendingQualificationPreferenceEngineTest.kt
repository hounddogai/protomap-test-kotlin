package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingQualificationPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingQualificationPreferenceEngine(newRegistry())
        val item = newPendingQualificationPreference("code-883", "name-994")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
