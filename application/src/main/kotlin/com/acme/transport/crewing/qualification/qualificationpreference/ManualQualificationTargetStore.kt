package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual qualification targets by code. */
interface ManualQualificationTargetStore {
    fun find(code: String): ManualQualificationTarget?
    fun save(item: ManualQualificationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualQualificationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualQualificationTargetStore(
    var items: MutableMap<String, ManualQualificationTarget>,
    var order: MutableList<String>,
) : ManualQualificationTargetStore {
    override fun find(code: String): ManualQualificationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualQualificationTarget) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<ManualQualificationTarget> {
        val out = mutableListOf<ManualQualificationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualQualificationTargetStore(): ManualQualificationTargetStore {
    return MemoryManualQualificationTargetStore(
        mutableMapOf<String, ManualQualificationTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualQualificationTargetStore(
    var inner: ManualQualificationTargetStore,
    var cache: TtlCache<ManualQualificationTarget>,
    var hits: Int,
    var misses: Int,
) : ManualQualificationTargetStore {
    override fun find(code: String): ManualQualificationTarget? {
        val hit = (cache.get(code) as ManualQualificationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualQualificationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualQualificationTarget> = inner.list()
}

fun newCachedManualQualificationTargetStore(inner: ManualQualificationTargetStore, ttl: Int, clock: Clock): ManualQualificationTargetStore {
    return CachedManualQualificationTargetStore(
        inner = inner,
        cache = newTtlCache<ManualQualificationTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual qualification targets. Empty fields match all. */
data class ManualQualificationTargetQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualQualificationTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualQualificationTarget>): MutableList<ManualQualificationTarget> {
        val matched = filter(items) { candidate: ManualQualificationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualQualificationTargets(store: ManualQualificationTargetStore, query: ManualQualificationTargetQuery): MutableList<ManualQualificationTarget> {
    return query.apply(store.list())
}
