package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingQualificationBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingQualificationBundleEngine(newRegistry())
        val item = newPendingQualificationBundle("code-390", "name-642")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
