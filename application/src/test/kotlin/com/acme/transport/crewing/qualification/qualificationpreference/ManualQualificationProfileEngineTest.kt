package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualQualificationProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualQualificationProfileEngine(newRegistry())
        val item = newManualQualificationProfile("code-598", "title-229")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
