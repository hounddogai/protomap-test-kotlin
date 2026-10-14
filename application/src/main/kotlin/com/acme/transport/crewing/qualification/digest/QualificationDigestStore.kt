package com.acme.transport.crewing.qualification.digest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification digests by code. */
interface QualificationDigestStore {
    fun find(code: String): QualificationDigest?
    fun save(item: QualificationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationDigestStore(
    var items: MutableMap<String, QualificationDigest>,
    var order: MutableList<String>,
) : QualificationDigestStore {
    override fun find(code: String): QualificationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationDigest) {
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

    override fun list(): MutableList<QualificationDigest> {
        val out = mutableListOf<QualificationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationDigestStore(): QualificationDigestStore {
    return MemoryQualificationDigestStore(mutableMapOf<String, QualificationDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationDigestStore(
    var inner: QualificationDigestStore,
    var cache: TtlCache<QualificationDigest>,
    var hits: Int,
    var misses: Int,
) : QualificationDigestStore {
    override fun find(code: String): QualificationDigest? {
        val hit = (cache.get(code) as QualificationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationDigest> = inner.list()
}

fun newCachedQualificationDigestStore(inner: QualificationDigestStore, ttl: Int, clock: Clock): QualificationDigestStore {
    return CachedQualificationDigestStore(
        inner = inner,
        cache = newTtlCache<QualificationDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification digests. Empty fields match all. */
data class QualificationDigestQuery(
    var region: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationDigest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationDigest>): MutableList<QualificationDigest> {
        val matched = filter(items) { candidate: QualificationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationDigests(store: QualificationDigestStore, query: QualificationDigestQuery): MutableList<QualificationDigest> {
    return query.apply(store.list())
}
