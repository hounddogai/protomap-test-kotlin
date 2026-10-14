package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalQualificationBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalQualificationBundleEngine(newRegistry())
        val item = newGlobalQualificationBundle("code-555", "name-306")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
