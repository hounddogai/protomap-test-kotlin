package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification bundles by code. */
interface QualificationBundleStore {
    fun find(code: String): QualificationBundle?
    fun save(item: QualificationBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationBundleStore(
    var items: MutableMap<String, QualificationBundle>,
    var order: MutableList<String>,
) : QualificationBundleStore {
    override fun find(code: String): QualificationBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationBundle) {
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

    override fun list(): MutableList<QualificationBundle> {
        val out = mutableListOf<QualificationBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationBundleStore(): QualificationBundleStore {
    return MemoryQualificationBundleStore(mutableMapOf<String, QualificationBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationBundleStore(
    var inner: QualificationBundleStore,
    var cache: TtlCache<QualificationBundle>,
    var hits: Int,
    var misses: Int,
) : QualificationBundleStore {
    override fun find(code: String): QualificationBundle? {
        val hit = (cache.get(code) as QualificationBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationBundle> = inner.list()
}

fun newCachedQualificationBundleStore(inner: QualificationBundleStore, ttl: Int, clock: Clock): QualificationBundleStore {
    return CachedQualificationBundleStore(
        inner = inner,
        cache = newTtlCache<QualificationBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification bundles. Empty fields match all. */
data class QualificationBundleQuery(
    var segment: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationBundle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationBundle>): MutableList<QualificationBundle> {
        val matched = filter(items) { candidate: QualificationBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationBundles(store: QualificationBundleStore, query: QualificationBundleQuery): MutableList<QualificationBundle> {
    return query.apply(store.list())
}
