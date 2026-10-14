package com.acme.transport.crewing.qualification.target

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification targets by code. */
interface QualificationTargetStore {
    fun find(code: String): QualificationTarget?
    fun save(item: QualificationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationTargetStore(
    var items: MutableMap<String, QualificationTarget>,
    var order: MutableList<String>,
) : QualificationTargetStore {
    override fun find(code: String): QualificationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationTarget) {
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

    override fun list(): MutableList<QualificationTarget> {
        val out = mutableListOf<QualificationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationTargetStore(): QualificationTargetStore {
    return MemoryQualificationTargetStore(mutableMapOf<String, QualificationTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationTargetStore(
    var inner: QualificationTargetStore,
    var cache: TtlCache<QualificationTarget>,
    var hits: Int,
    var misses: Int,
) : QualificationTargetStore {
    override fun find(code: String): QualificationTarget? {
        val hit = (cache.get(code) as QualificationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationTarget> = inner.list()
}

fun newCachedQualificationTargetStore(inner: QualificationTargetStore, ttl: Int, clock: Clock): QualificationTargetStore {
    return CachedQualificationTargetStore(
        inner = inner,
        cache = newTtlCache<QualificationTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification targets. Empty fields match all. */
data class QualificationTargetQuery(
    var source: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationTarget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationTarget>): MutableList<QualificationTarget> {
        val matched = filter(items) { candidate: QualificationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationTargets(store: QualificationTargetStore, query: QualificationTargetQuery): MutableList<QualificationTarget> {
    return query.apply(store.list())
}
