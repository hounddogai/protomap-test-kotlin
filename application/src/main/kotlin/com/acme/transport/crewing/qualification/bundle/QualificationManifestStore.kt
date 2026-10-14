package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification manifests by code. */
interface QualificationManifestStore {
    fun find(code: String): QualificationManifest?
    fun save(item: QualificationManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationManifestStore(
    var items: MutableMap<String, QualificationManifest>,
    var order: MutableList<String>,
) : QualificationManifestStore {
    override fun find(code: String): QualificationManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationManifest) {
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

    override fun list(): MutableList<QualificationManifest> {
        val out = mutableListOf<QualificationManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationManifestStore(): QualificationManifestStore {
    return MemoryQualificationManifestStore(
        mutableMapOf<String, QualificationManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationManifestStore(
    var inner: QualificationManifestStore,
    var cache: TtlCache<QualificationManifest>,
    var hits: Int,
    var misses: Int,
) : QualificationManifestStore {
    override fun find(code: String): QualificationManifest? {
        val hit = (cache.get(code) as QualificationManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationManifest> = inner.list()
}

fun newCachedQualificationManifestStore(inner: QualificationManifestStore, ttl: Int, clock: Clock): QualificationManifestStore {
    return CachedQualificationManifestStore(
        inner = inner,
        cache = newTtlCache<QualificationManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification manifests. Empty fields match all. */
data class QualificationManifestQuery(
    var segment: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationManifest>): MutableList<QualificationManifest> {
        val matched = filter(items) { candidate: QualificationManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationManifests(store: QualificationManifestStore, query: QualificationManifestQuery): MutableList<QualificationManifest> {
    return query.apply(store.list())
}
