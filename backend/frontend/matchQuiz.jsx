import React, { useState } from "react";

const Card = ({ children, className }) => (
  <div className={`p-6 bg-mint-500 shadow-lg rounded-2xl ${className}`}>{children}</div>
);

const CardContent = ({ children }) => <div className="p-4">{children}</div>;

const Button = ({ children, onClick, variant, type = "button" }) => (
  <button
    type={type}
    onClick={onClick}
    className={`px-4 py-2 rounded-lg text-white transition-all font-semibold ${
      variant === "destructive" ? "bg-red-600 hover:bg-red-700" : "bg-green-600 hover:bg-green-700"
    }`}
  >
    {children}
  </button>
);

const Input = ({ name, value, onChange, type = "text" }) => (
  <input
    name={name}
    value={value}
    onChange={onChange}
    type={type}
    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-400"
  />
);

const Label = ({ children }) => <label className="block font-semibold mb-1 text-green-800">{children}</label>;

const Select = ({ name, value, onChange, options }) => (
  <select
    name={name}
    value={value}
    onChange={onChange}
    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-400"
  >
    {options.map((option, index) => (
      <option key={index} value={option}>{option}</option>
    ))}
  </select>
);

const ProfilePage = () => {
  const [profile, setProfile] = useState({
    name: "",
    email: "",
    majorSchool: "",
    major: "",
    strengths: "",
    weaknesses: "",
    day: "Monday",
    time: "12 AM",
    gradeLevel: "1st Year"
  });

  const daysOfWeek = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
  const timeSlots = Array.from({ length: 24 }, (_, i) => `${i === 0 ? 12 : i % 12} ${i < 12 ? "AM" : "PM"}`);
  const majorSchools = ["School of Engineering", "School of Architecture", "School of Art"];
  const engineeringMajors = ["Electrical Engineering", "Mechanical Engineering", "Chemical Engineering", "Civil Engineering"];

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile((prev) => ({ ...prev, [name]: value, ...(name === "majorSchool" && value !== "School of Engineering" ? { major: "" } : {}) }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Form submitted:", profile);
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-mint-200 p-4">
      <Card className="w-full max-w-md bg-mint-300 shadow-lg p-6 rounded-2xl">
        <CardContent>
          <h2 className="text-xl font-bold mb-4 text-green-900">Quiz Form</h2>
          <form onSubmit={handleSubmit} className="space-y-3">
            <div>
              <Label>Name</Label>
              <Input name="name" value={profile.name} onChange={handleChange} />
            </div>
            <div>
              <Label>Email</Label>
              <Input name="email" type="email" value={profile.email} onChange={handleChange} />
            </div>
            <div>
              <Label>School</Label>
              <Select name="majorSchool" value={profile.majorSchool} onChange={handleChange} options={majorSchools} />
            </div>
            {profile.majorSchool === "School of Engineering" && (
              <div>
                <Label>Engineering Major</Label>
                <Select name="major" value={profile.major} onChange={handleChange} options={engineeringMajors} />
              </div>
            )}
            <div>
              <Label>Strengths</Label>
              <Input name="strengths" value={profile.strengths} onChange={handleChange} />
            </div>
            <div>
              <Label>Weaknesses</Label>
              <Input name="weaknesses" value={profile.weaknesses} onChange={handleChange} />
            </div>
            <div>
              <Label>Available Day</Label>
              <Select name="day" value={profile.day} onChange={handleChange} options={daysOfWeek} />
            </div>
            <div>
              <Label>Available Time</Label>
              <Select name="time" value={profile.time} onChange={handleChange} options={timeSlots} />
            </div>
            <div>
              <Label>Grade Level</Label>
              <Select
                name="gradeLevel"
                value={profile.gradeLevel}
                onChange={handleChange}
                options={["1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year"]}
              />
            </div>
            <div className="flex justify-between mt-4">
              <Button type="submit">Submit</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default ProfilePage;
