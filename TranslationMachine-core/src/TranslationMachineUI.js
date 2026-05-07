import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export default function TranslationMachineUI() {
    const [songs, setSongs] = useState([
        { title: "", artist: "" },
        { title: "", artist: "" },
        { title: "", artist: "" }
    ]);
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);

    const handleInputChange = (index, field, value) => {
        const updatedSongs = [...songs];
        updatedSongs[index][field] = value;
        setSongs(updatedSongs);
    };

    const fetchSongData = async () => {
        setLoading(true);
        setResults([]);

        const fetchedResults = [];

        for (const { title, artist } of songs) {
            if (!title || !artist) continue;

            // Simulating API call
            const response = await fetch(`/api/analyze?song=${title}&artist=${artist}`);
            const data = await response.json();
            fetchedResults.push({ title, artist, analysis: data });
        }

        setResults(fetchedResults);
        setLoading(false);
    };

    return (
        <div className="p-4 max-w-3xl mx-auto">
            <h1 className="text-2xl font-bold mb-4">Translation Machine</h1>
            {songs.map((song, index) => (
                <div key={index} className="mb-4">
                    {index === 0 && (
                        <>
                            <Input
                                placeholder="Enter song title of your most recently played song"
                                value={song.title}
                                onChange={(e) => handleInputChange(index, "title", e.target.value)}
                                className="mb-2"
                            />
                            <Input
                                placeholder={`Enter artist for '${song.title}'`}
                                value={song.artist}
                                onChange={(e) => handleInputChange(index, "artist", e.target.value)}
                            />
                        </>
                    )}
                    {index === 1 && (
                        <>
                            <Input
                                placeholder="Enter song title of your favorite song today"
                                value={song.title}
                                onChange={(e) => handleInputChange(index, "title", e.target.value)}
                                className="mb-2"
                            />
                            <Input
                                placeholder={`Enter artist for '${song.title}'`}
                                value={song.artist}
                                onChange={(e) => handleInputChange(index, "artist", e.target.value)}
                            />
                        </>
                    )}
                    {index === 2 && (
                        <>
                            <Input
                                placeholder="Enter song title of your favorite song this week"
                                value={song.title}
                                onChange={(e) => handleInputChange(index, "title", e.target.value)}
                                className="mb-2"
                            />
                            <Input
                                placeholder={`Enter artist for '${song.title}'`}
                                value={song.artist}
                                onChange={(e) => handleInputChange(index, "artist", e.target.value)}
                            />
                        </>
                    )}
                </div>
            ))}
            <Button onClick={fetchSongData} disabled={loading} className="mt-4 w-full">
                {loading ? "Analyzing..." : "Analyze Songs"}
            </Button>
            <div className="mt-6">
                {results.map((result, index) => (
                    <Card key={index} className="mb-4">
                        <CardContent className="p-4">
                            <h2 className="text-xl font-semibold">Main Ideas for '{result.title}' by {result.artist}:</h2>
                            <div className="mt-2">
                                {result.analysis.substrings.map((item, i) => (
                                    <div key={i} className="mb-4">
                                        <p className="font-medium">Substring: {item.substring}</p>
                                        <p>Main Idea (Intent): {item.intent}</p>
                                        <p>Color Description: {item.colorDescription}</p>
                                        <div className="w-full h-10 mt-1" style={{ backgroundColor: item.hexCode }}></div>
                                    </div>
                                ))}
                                {result.analysis.honorableMention && (
                                    <div className="mt-4">
                                        <p className="font-medium">Honorable Mention: {result.analysis.honorableMention.substring}</p>
                                        <p>Main Idea (Intent): {result.analysis.honorableMention.intent}</p>
                                        <p>Color Description: {result.analysis.honorableMention.colorDescription}</p>
                                        <div className="w-full h-10 mt-1" style={{ backgroundColor: result.analysis.honorableMention.hexCode }}></div>
                                    </div>
                                )}
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
}