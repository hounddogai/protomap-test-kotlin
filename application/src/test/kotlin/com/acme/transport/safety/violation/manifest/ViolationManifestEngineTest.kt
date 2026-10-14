package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationManifestEngine()
        val item = newViolationManifest("code-977", "name-508")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
